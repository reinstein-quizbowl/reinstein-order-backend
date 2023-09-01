package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.PacketExposure
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PacketExposureRepository : CrudRepository<PacketExposure, Long> {
    fun findByPacketIdIn(packetIds: List<Long>): List<PacketExposure>

    fun findByExposedSchoolIdAndPacketId(exposedSchoolId: Long, packetId: Long): List<PacketExposure>
}
