package lionel.meethere.order.service;

import lionel.meethere.booking.service.SiteBookTimeService;
import lionel.meethere.order.dao.SiteBookingOrderMapper;
import lionel.meethere.order.entity.SiteBookingOrder;
import lionel.meethere.order.exception.BookingTimeConflictException;
import lionel.meethere.order.exception.UserIdNotMatchOrderException;
import lionel.meethere.order.exception.WrongOrderStatusException;
import lionel.meethere.order.param.SiteBookingOrderCreateParam;
import lionel.meethere.order.param.SiteBookingOrderUpdateParam;
import lionel.meethere.order.status.AuditStatus;
import lionel.meethere.order.status.OrderStatus;
import lionel.meethere.order.vo.SiteBookingOrderAdminVO;
import lionel.meethere.order.vo.SiteBookingOrderUserVO;
import lionel.meethere.paging.PageParam;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class SiteBookingOrderService {

    @Autowired
    private SiteBookingOrderMapper siteBookingOrderMapper;

    @Autowired
    private SiteBookTimeService siteBookTimeService;


    public void crateSiteBookingOrder(Integer userId, SiteBookingOrderCreateParam createParam){
        if(!siteBookTimeService.tryBooking(createParam.getSiteId(),createParam.getStartTime(),createParam.getEndTime())){
            throw new BookingTimeConflictException();
        }
        siteBookTimeService.insertBokingTime(createParam.getSiteId(),createParam.getStartTime(),createParam.getEndTime());
        siteBookingOrderMapper.insertOrder(convertToSiteBookingOrder(userId,createParam));
    }

    private SiteBookingOrder convertToSiteBookingOrder(Integer userId,SiteBookingOrderCreateParam createParam){
        SiteBookingOrder siteBookingOrder = new SiteBookingOrder();
        BeanUtils.copyProperties(createParam,siteBookingOrder);
        siteBookingOrder.setUserId(userId);
        siteBookingOrder.setStatus(OrderStatus.UNAUDITED);

        return siteBookingOrder;
    }

    public void auditOrder(Integer orderId, Integer auditStatus){
        SiteBookingOrder order = siteBookingOrderMapper.getOrderById(orderId);

        if(!order.getStatus().equals(OrderStatus.UNAUDITED)){
            throw new WrongOrderStatusException();
        }

        if(auditStatus.equals(AuditStatus.SUCCESS)){
            siteBookingOrderMapper.updateOrderStatus(orderId,OrderStatus.AUDITED);
        }
        else {
            siteBookingOrderMapper.updateOrderStatus(orderId,OrderStatus.AUDITED_FAILED);
        }

    }

    public void cancelOrderByUser(Integer userId, Integer orderId){
        SiteBookingOrder order = siteBookingOrderMapper.getOrderById(orderId);
        System.out.println("userID"+userId+"orderID"+orderId+"  "+order.getUserId());
        if(!userId.equals(order.getUserId()))
            throw new UserIdNotMatchOrderException();

        if(!order.getStatus().equals(OrderStatus.CANCEL)){
            siteBookingOrderMapper.updateOrderStatus(orderId,OrderStatus.CANCEL);
            siteBookTimeService.cancelSiteBookTime(order.getSiteId(),order.getStartTime());
        }

    }

    public void cancelOrderByAdmin(Integer orderId){
        SiteBookingOrder order = siteBookingOrderMapper.getOrderById(orderId);

        if(!order.getStatus().equals(OrderStatus.CANCEL)){
            siteBookingOrderMapper.updateOrderStatus(orderId,OrderStatus.CANCEL);
            siteBookTimeService.cancelSiteBookTime(order.getSiteId(),order.getStartTime());
        }
    }

    public void updateOrderBookTime(SiteBookingOrderUpdateParam updateParam){
        if(!siteBookTimeService.tryBooking(updateParam.getSiteId(),updateParam.getStartTime(),updateParam.getEndTime())){
            throw new BookingTimeConflictException();
        }
        siteBookTimeService.updateSiteBookTime(updateParam);
        siteBookingOrderMapper.updateOrderBookTime(updateParam);
        siteBookingOrderMapper.updateOrderStatus(updateParam.getOrderId(),OrderStatus.UNAUDITED);

    }

    public SiteBookingOrderAdminVO getOrderById(Integer id){
        return convertToSiteBookingOrderAdminVO(siteBookingOrderMapper.getOrderById(id));
    }

    public List<SiteBookingOrderUserVO> getOrderByUser(Integer userId, Integer status, PageParam pageParam){
        System.out.println(pageParam);
        return siteBookingOrderMapper.getOrderByUser(userId,status,pageParam);
    }

    public List<SiteBookingOrderAdminVO> getOrderBySite(Integer userId, Integer status, PageParam pageParam){
        return siteBookingOrderMapper.getOrderBySite(userId,status,pageParam);
    }


    private SiteBookingOrderAdminVO convertToSiteBookingOrderAdminVO(SiteBookingOrder siteBookingOrder){
        SiteBookingOrderAdminVO orderAdminVO = new SiteBookingOrderAdminVO();
        BeanUtils.copyProperties(siteBookingOrder,orderAdminVO);
        return orderAdminVO;
    }

    public int getOrderCount(){
        return siteBookingOrderMapper.getOrderCount();
    }

}
