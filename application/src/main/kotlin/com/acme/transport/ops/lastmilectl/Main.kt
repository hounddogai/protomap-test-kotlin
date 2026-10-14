package com.acme.transport.ops.lastmilectl

import com.acme.transport.kit.flags.newRegistry
import com.acme.transport.lastmile.clock.system
import com.acme.transport.lastmile.drop.exception.buildDropSnapshotReport
import com.acme.transport.lastmile.drop.exception.newDropSnapshot
import com.acme.transport.lastmile.drop.exception.newDropSnapshotCoordinator
import com.acme.transport.lastmile.drop.exception.newMemoryDropSnapshotStore
import com.acme.transport.lastmile.parcel.segment.buildParcelDigestReport
import com.acme.transport.lastmile.parcel.segment.newMemoryParcelDigestStore
import com.acme.transport.lastmile.parcel.segment.newParcelDigest
import com.acme.transport.lastmile.parcel.segment.newParcelDigestWorkflow
import com.acme.transport.lastmile.proofofdelivery.mapping.buildProofOfDeliveryDigestReport
import com.acme.transport.lastmile.proofofdelivery.mapping.newMemoryProofOfDeliveryDigestStore
import com.acme.transport.lastmile.proofofdelivery.mapping.newProofOfDeliveryDigest
import com.acme.transport.lastmile.proofofdelivery.mapping.newProofOfDeliveryDigestService
import kotlin.system.exitProcess

fun main() {
    val clock = system()
    val commands = mutableMapOf<String, () -> Boolean>(
        "drop-snapshot" to fun(): Boolean {
            val store = newMemoryDropSnapshotStore()
            for (index in 0 until 7) {
                store.save(newDropSnapshot("drop-snapshot-$index", "Sample drop snapshot $index"))
            }
            val flow = newDropSnapshotCoordinator(store, clock, newRegistry())
            try {
                flow.start("drop-snapshot-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildDropSnapshotReport("Drop Snapshot", items, clock)
            println(report.render())
            return flow.scan((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
        "parcel-digest" to fun(): Boolean {
            val store = newMemoryParcelDigestStore()
            for (index in 0 until 12) {
                store.save(newParcelDigest("parcel-digest-$index", "Sample parcel digest $index"))
            }
            val flow = newParcelDigestWorkflow(store, clock)
            try {
                flow.submit("parcel-digest-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildParcelDigestReport("Parcel Digest", items, clock)
            println(report.render())
            return flow.sweep((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
        "proof-of-delivery-digest" to fun(): Boolean {
            val store = newMemoryProofOfDeliveryDigestStore()
            for (index in 0 until 5) {
                store.save(newProofOfDeliveryDigest("proof-of-delivery-digest-$index", "Sample proof of delivery digest $index"))
            }
            val flow = newProofOfDeliveryDigestService(store, clock, newRegistry())
            try {
                flow.advance("proof-of-delivery-digest-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildProofOfDeliveryDigestReport("Proof Of Delivery Digest", items, clock)
            println(report.render())
            return flow.scan((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
    )
    var failed = 0
    for ((name, command) in commands.toSortedMap()) {
        if (!command()) {
            println("$name failed")
            failed += 1
        }
    }
    if (failed > 0) {
        exitProcess(1)
    }
}
