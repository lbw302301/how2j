package filter;


import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BackServletFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        String contextPath = request.getServletContext().getContextPath();
        String uri = request.getRequestURI();
        uri = StringUtils.remove(uri,contextPath);
        if(uri.startsWith("/admin")){
            String servletPath = StringUtils.substringBetween(uri,"_","_")+"Servlet";
            String method = StringUtils.substringAfterLast(uri,"_");
            request.setAttribute("method",method);
            req.getRequestDispatcher("/"+servletPath).forward(request,response);
            return;
        }
        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {

    }
}
