package servlet;

import bean.Category;
import bean.Product;
import bean.Property;
import bean.PropertyValue;
import util.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

public class ProductServlet extends BaseBackServlet{
    @Override
    public String add(HttpServletRequest request, HttpServletResponse response, Page page) {
        String name = request.getParameter("name");
        String subtitle = request.getParameter("subTitle");
        float originalPrice = Float.parseFloat(request.getParameter("orignalPrice"));
        float promotePrice = Float.parseFloat(request.getParameter("promotePrice"));
        int stock = Integer.parseInt(request.getParameter("stock"));
        int cid = Integer.parseInt(request.getParameter("cid"));
        Category c = categoryDAO.get(cid);
        Product p = new Product();
        p.setCategory(c);
        p.setName(name);
        p.setSubTitle(subtitle);
        p.setOrignalPrice(originalPrice);
        p.setPromotePrice(promotePrice);
        p.setCreateDate(new Date());
        p.setStock(stock);
        productDAO.add(p);
        return "@admin_product_list?cid="+cid;
    }

    @Override
    public String delete(HttpServletRequest request, HttpServletResponse response, Page page) {
        int id = Integer.parseInt(request.getParameter("id"));
        Product p = productDAO.get(id);
        productDAO.delete(id);
        return "@admin_product_list?cid="+p.getCategory().getId();
    }

    @Override
    public String edit(HttpServletRequest request, HttpServletResponse response, Page page) {
        int id = Integer.parseInt(request.getParameter("id"));
        Product p = productDAO.get(id);
        request.setAttribute("p",p);
        return "admin/editProduct.jsp";
    }

    @Override
    public String update(HttpServletRequest request, HttpServletResponse response, Page page) {
        int cid = Integer.parseInt(request.getParameter("cid"));
        Category c = categoryDAO.get(cid);

        int id = Integer.parseInt(request.getParameter("id"));
        int stock = Integer.parseInt(request.getParameter("stock"));
        float orignalPrice = Float.parseFloat(request.getParameter("orignalPrice"));
        float promotePrice = Float.parseFloat(request.getParameter("promotePrice"));
        String subTitle= request.getParameter("subTitle");
        String name= request.getParameter("name");

        Product p = new Product();

        p.setName(name);
        p.setSubTitle(subTitle);
        p.setOrignalPrice(orignalPrice);
        p.setPromotePrice(promotePrice);
        p.setStock(stock);
        p.setId(id);
        p.setCategory(c);

        productDAO.update(p);
        return "@admin_product_list?cid="+p.getCategory().getId();
    }

    @Override
    public String list(HttpServletRequest request, HttpServletResponse response, Page page) {
        int cid = Integer.parseInt(request.getParameter("cid"));
        Category c = categoryDAO.get(cid);
        List<Product> ps = productDAO.list(cid,page.getStart(),page.getCount());
        int total = productDAO.getTotal(cid);
        page.setTotal(total);
        page.setParam("&cid="+c.getId());
        request.setAttribute("page",page);
        request.setAttribute("c",c);
        request.setAttribute("ps",ps);
        return "admin/listProduct.jsp";
    }

    public String editPropertyValue(HttpServletRequest request, HttpServletResponse response, Page page){
        int id = Integer.parseInt(request.getParameter("id"));
        Product p = productDAO.get(id);
        propertyValueDAO.init(p);
        List<PropertyValue> pvs = propertyValueDAO.list(p.getId());
        request.setAttribute("p",p);
        request.setAttribute("pvs",pvs);
        return "admin/editProductValue.jsp";
    }

    public String updatePropertyValue(HttpServletRequest request, HttpServletResponse response, Page page){
        int pvid = Integer.parseInt(request.getParameter("pvid"));
        String value = request.getParameter("value");
        PropertyValue pv = propertyValueDAO.get(pvid);
        pv.setValue(value);
        propertyValueDAO.update(pv);
        return "%success";
    }
}
