package servlet;
import bean.*;
import comparator.*;
import dao.CategoryDAO;
import dao.OrderDAO;
import dao.ProductDAO;
import dao.ProductImageDAO;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.web.util.HtmlUtils;
import util.Page;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
public class ForeServlet extends BaseForeServlet{
    public String home(HttpServletRequest request, HttpServletResponse response, Page page) {
        List<Category> cs= new CategoryDAO().list();
        new ProductDAO().fill(cs);
        new ProductDAO().fillByRow(cs);
        request.setAttribute("cs", cs);
        return "home.jsp";
    }

    public String register(HttpServletRequest request, HttpServletResponse response, Page page){
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        name = HtmlUtils.htmlEscape(name);
        System.out.println(name);
        boolean exist = userDAO.isExist(name);
        if(exist){
            request.setAttribute("msg", "用户名已经被使用，不能使用");
            return "register.jsp";
        }
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        userDAO.add(user);

        return "@registerSuccess.jsp";
    }

    public String login(HttpServletRequest request, HttpServletResponse response, Page page){
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        name = HtmlUtils.htmlEscape(name);
        User user = userDAO.get(name, password);
        if(null == user){
            request.setAttribute("msg","账号密码错误");
            return "login.jsp";
        }
        request.getSession().setAttribute("user",user);
        return "@forehome";
    }

    public String logout(HttpServletRequest request, HttpServletResponse response, Page page){
        request.getSession().removeAttribute("user");
        return "@login.jsp";
    }

    public String product(HttpServletRequest request, HttpServletResponse response, Page page){
        int pid = Integer.parseInt(request.getParameter("pid"));
        Product p = productDAO.get(pid);
        List<ProductImage> productSingleImages = productImageDAO.list(p, ProductImageDAO.type_single);
        List<ProductImage> productDetailImages = productImageDAO.list(p, ProductImageDAO.type_detail);
        p.setProductSingleImages(productSingleImages);
        p.setProductDetailImages(productDetailImages);
        List<PropertyValue> pvs = propertyValueDAO.list(p.getId());
        List<Review> reviews = reviewDAO.list(p.getId());
        productDAO.setSaleAndReviewNumber(p);
        request.setAttribute("reviews", reviews);
        request.setAttribute("p", p);
        request.setAttribute("pvs", pvs);
        return "product.jsp";
    }

    public String checkLogin(HttpServletRequest request, HttpServletResponse response, Page page){
        User user = (User)request.getSession().getAttribute("user");
        if(null != user){
            return "%success";
        }
        return "%fail";
    }

    public String loginAjax(HttpServletRequest request, HttpServletResponse response, Page page){
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        User user = userDAO.get(name, password);
        if(user != null){
            request.getSession().setAttribute("user",user);
            return "%success";
        }
        return "%fail";
    }

    public String category(HttpServletRequest request, HttpServletResponse response, Page page){
        int cid = Integer.parseInt(request.getParameter("cid"));
        Category c = categoryDAO.get(cid);
        productDAO.fill(c);
        productDAO.setSaleAndReviewNumber(c.getProducts());
        String sort = request.getParameter("sort");
        if(sort!=null){
            switch (sort){
                case "review":
                    Collections.sort(c.getProducts(),new ProductReviewComparator());
                    break;
                case "date" :
                    Collections.sort(c.getProducts(),new ProductDateComparator());
                    break;

                case "saleCount" :
                    Collections.sort(c.getProducts(),new ProductSaleCountComparator());
                    break;

                case "price":
                    Collections.sort(c.getProducts(),new ProductPriceComparator());
                    break;

                case "all":
                    Collections.sort(c.getProducts(),new ProductAllComparator());
                    break;
            }
        }
        request.setAttribute("C",c);
        return "category.jsp";
    }

    public String search(HttpServletRequest request, HttpServletResponse response, Page page){
        String keyword = request.getParameter("keyword");
        List<Product> ps = productDAO.search(keyword, 0,20);
        productDAO.setSaleAndReviewNumber(ps);
        request.setAttribute("ps",ps);
        return "searchResult.jsp";
    }

    public String buyone(HttpServletRequest request, HttpServletResponse response, Page page){
        int pid = Integer.parseInt(request.getParameter("pid"));
        int num = Integer.parseInt(request.getParameter("num"));
        Product p = productDAO.get(pid);
        int oiid = 0;
        User user = (User)request.getSession().getAttribute("user");
        boolean found = false;
        List<OrderItem> ois = orderItemDAO.listByUser(user.getId());
        for (OrderItem oi : ois) {
            if(oi.getProduct().getId()==p.getId()){
                oi.setNumber(oi.getNumber()+num);
                orderItemDAO.update(oi);
                found = true;
                oiid = oi.getId();
                break;
            }
        }
        if(!found){
            OrderItem oi = new OrderItem();
            oi.setUser(user);
            oi.setNumber(num);
            oi.setProduct(p);
            orderItemDAO.add(oi);
            oiid = oi.getId();
        }
        return "@forebuy?oiid="+oiid;
    }

    public String buy(HttpServletRequest request, HttpServletResponse response, Page page){
        String[] oiids = request.getParameterValues("oiid");
        List<OrderItem> ois = new ArrayList<>();
        float total = 0;
        for(String strid : oiids){
            int oiid = Integer.parseInt(strid);
            OrderItem oi = orderItemDAO.get(oiid);
            total += oi.getProduct().getPromotePrice()*oi.getNumber();
            ois.add(oi);
        }
        request.getSession().setAttribute("ois",ois);
        request.setAttribute("total",total);
        return "buy.jsp";
    }

    public String addCart(HttpServletRequest request, HttpServletResponse response, Page page){
        int pid = Integer.parseInt(request.getParameter("pid"));
        int num = Integer.parseInt(request.getParameter("num"));
        Product p = productDAO.get(pid);
        User user = (User)request.getSession().getAttribute("user");
        boolean found = false;
        List<OrderItem> ois = orderItemDAO.listByUser(user.getId());
        for(OrderItem oi :ois){
            if(oi.getProduct().getId() == p.getId()){
                oi.setNumber(oi.getNumber()+num);
                orderItemDAO.update(oi);
                found = true;
                break;
            }
        }
        if(!found){
            OrderItem oi = new OrderItem();
            oi.setUser(user);
            oi.setNumber(num);
            oi.setProduct(p);
            orderItemDAO.add(oi);
        }
        return "%success";
    }

    public String cart(HttpServletRequest request, HttpServletResponse response, Page page){
        User user = (User)request.getSession().getAttribute("user");
        List<OrderItem> ois = orderItemDAO.listByUser(user.getId());
        request.setAttribute("ois",ois);
        return "cart.jsp";
    }

    public String changeOrderItem(HttpServletRequest request, HttpServletResponse response, Page page){
        int pid = Integer.parseInt(request.getParameter("pid"));
        int num = Integer.parseInt(request.getParameter("num"));
        User user = (User)request.getSession().getAttribute("user");
        if(null==user){
            return "%fail";
        }
        List<OrderItem> ois = orderItemDAO.listByUser(user.getId());
        for(OrderItem oi : ois){
            if(oi.getProduct().getId() == pid){
                oi.setNumber(num);
                orderItemDAO.update(oi);
                break;
            }
        }
        return "%success";
    }

    public String deleteOrderItem(HttpServletRequest request, HttpServletResponse response, Page page){
        User user =(User) request.getSession().getAttribute("user");
        if(null==user)
            return "%fail";
        int oiid = Integer.parseInt(request.getParameter("oiid"));
        orderItemDAO.delete(oiid);
        return "%success";
    }

    public String creatOrder(HttpServletRequest request, HttpServletResponse response, Page page){
        User user = (User)request.getSession().getAttribute("user");
        List<OrderItem> ois = (List<OrderItem>)request.getSession().getAttribute("ois");
        if(ois.isEmpty())
            return "@login.jsp";
        String address = request.getParameter("address");
        String post = request.getParameter("post");
        String receiver = request.getParameter("receiver");
        String mobile = request.getParameter("mobile");
        String userMessage = request.getParameter("userMessage");

        Order order = new Order();
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);

        order.setOrderCode(orderCode);
        order.setAddress(address);
        order.setPost(post);
        order.setReceiver(receiver);
        order.setMobile(mobile);
        order.setUserMessage(userMessage);
        order.setCreateDate(new Date());
        order.setUser(user);
        order.setStatus(orderDAO.waitPay);
        orderDAO.add(order);
        float total = 0;
        for(OrderItem oi : ois){
            oi.setOrder(order);
            orderItemDAO.update(oi);
            total += oi.getProduct().getPromotePrice() * oi.getNumber();
        }
        return "@forealipay?oid="+order.getId()+"&total="+total;
    }

    public String alipay(HttpServletRequest request, HttpServletResponse response, Page page){
        return "alipay.jsp";
    }

    public String payed(HttpServletRequest request, HttpServletResponse response, Page page){
        int oid = Integer.parseInt(request.getParameter("oid"));
        Order o = orderDAO.get(oid);
        o.setStatus(OrderDAO.waitDelivery);
        o.setPayDate(new Date());
        orderDAO.update(o);
        request.setAttribute("o",o);
        return "payed.jsp";
    }
    public String bought(HttpServletRequest request, HttpServletResponse response, Page page){
        User user =(User) request.getSession().getAttribute("user");
        List<Order> os= orderDAO.list(user.getId(),OrderDAO.delete);
        orderItemDAO.fill(os);
        request.setAttribute("os", os);
        return "bought.jsp";
    }

    public String confirmPay(HttpServletRequest request, HttpServletResponse response, Page page){
        int oid = Integer.parseInt(request.getParameter("oid"));
        Order o = orderDAO.get(oid);
        orderItemDAO.fill(o);
        request.setAttribute("o",o);
        return "confirmPay.jsp";
    }

    public String orderConfirmed(HttpServletRequest request, HttpServletResponse response, Page page){
        int oid = Integer.parseInt(request.getParameter("oid"));
        Order o = orderDAO.get(oid);
        o.setStatus(OrderDAO.waitReview);
        o.setConfirmDate(new Date());
        orderDAO.update(o);
        return "orderConfirmed.jsp";
    }

    public String deleteOrder(HttpServletRequest request, HttpServletResponse response, Page page){
        int oid = Integer.parseInt(request.getParameter("oid"));
        Order o = orderDAO.get(oid);
        o.setStatus(OrderDAO.delete);
        orderDAO.update(o);
        return "%success";
    }

    public String review(HttpServletRequest request, HttpServletResponse response, Page page){
        int oid = Integer.parseInt(request.getParameter("oid"));
        Order o = orderDAO.get(oid);
        orderItemDAO.fill(o);
        Product p = o.getOrderItems().get(0).getProduct();
        List<Review> reviews = reviewDAO.list(p.getId());
        productDAO.setSaleAndReviewNumber(p);
        request.setAttribute("p", p);
        request.setAttribute("o", o);
        request.setAttribute("reviews", reviews);
        return "review.jsp";
    }
    public String doreview(HttpServletRequest request, HttpServletResponse response, Page page){
        int oid = Integer.parseInt(request.getParameter("oid"));
        int pid = Integer.parseInt(request.getParameter("pid"));
        Order o = orderDAO.get(oid);
        o.setStatus(OrderDAO.finish);
        orderDAO.update(o);
        Product p = productDAO.get(pid);
        String content = request.getParameter("content");
        content = HtmlUtils.htmlEscape(content);
        User user = (User)request.getSession().getAttribute("user");
        Review review = new Review();
        review.setContent(content);
        review.setCreateDate(new Date());
        review.setProduct(p);
        review.setUser(user);
        reviewDAO.add(review);
        return "@forereview?oid="+oid+"&showonly=true";
    }
}
