package cn.springcloud.gray.server.netflix.eureka;

import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.server.configuration.properties.GrayServerProperties;
import cn.springcloud.gray.server.evictor.GrayServerEvictor;
import cn.springcloud.gray.server.module.GrayServerModule;
import cn.springcloud.gray.server.module.domain.GrayInstance;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

import java.util.Objects;


/**
 * 依赖EurekaClient来检查服务实例是否下线,建议使用DefaultGrayServiceEvictor
 */
public class EurekaGrayServerEvictor implements GrayServerEvictor {

    private EurekaClient eurekaClient;
    private GrayServerProperties grayServerProperties;


    public EurekaGrayServerEvictor(EurekaClient eurekaClient) {
        this.eurekaClient = eurekaClient;
    }

    private void evict(GrayServerModule grayServerModule, InstanceInfo instanceInfo, GrayInstance grayInstance) {
        InstanceStatus instanceStatus = getInstanceStatus(instanceInfo);
        if (!Objects.equals(grayInstance.getInstanceStatus(), instanceStatus)) {
            grayServerModule.updateInstanceStatus(grayInstance.getInstanceId(), instanceStatus);
        }
    }


    private InstanceStatus getInstanceStatus(InstanceInfo instanceInfo) {
        if (instanceInfo == null) {
            return InstanceStatus.DOWN;
        }
        InstanceInfo.InstanceStatus status = instanceInfo.getStatus();
        return EurekaInstatnceTransformer.toGrayInstanceStatus(status);
    }


    @Override
    public void evict(GrayServerModule grayServerModule) {
        grayServerModule.allGrayServices().forEach(grayService -> {
            Application app = eurekaClient.getApplication(grayService.getServiceId());
            if (app != null) {
                grayServerModule.listGrayInstancesByServiceId(grayService.getServiceId()).forEach(instance -> {
                    evict(grayServerModule, app.getByInstanceId(instance.getInstanceId()), instance);
                });
            }
        });
    }
}
