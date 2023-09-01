package com.reinsteinquizbowl.order.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable

@Entity
@Immutable // view
data class PacketExposure(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null, // not necessarily consistent from query to query, but JPA needs some kind of ID
    var packetId: Long,
    var exposedSchoolId: Long,
    var source: String,
    var bookingId: Long,
    var ordererSchoolId: Long,
    var tentativePacketExposure: Boolean,
    var confirmedPacketExposure: Boolean,
)
