package org.example.feign.base;

import org.example.entity.IpInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ip-api", url = "http://ip-api.com/json/")
public interface IpServiceFeignClient {

    @GetMapping("/{ip}")
    ResponseEntity<IpInfo> getIPInfo(@PathVariable String ipAddress);
}
