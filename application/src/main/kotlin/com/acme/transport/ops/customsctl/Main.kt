package com.acme.transport.ops.customsctl

import com.acme.transport.customs.broker.estimate.buildBrokerEstimateReport
import com.acme.transport.customs.broker.estimate.newBrokerEstimate
import com.acme.transport.customs.broker.estimate.newBrokerEstimateWorkflow
import com.acme.transport.customs.broker.estimate.newMemoryBrokerEstimateStore
import com.acme.transport.customs.clock.system
import com.acme.transport.customs.declaration.review.buildDeclarationReviewReport
import com.acme.transport.customs.declaration.review.newDeclarationReview
import com.acme.transport.customs.declaration.review.newDeclarationReviewManager
import com.acme.transport.customs.declaration.review.newMemoryDeclarationReviewStore
import kotlin.system.exitProcess

fun main() {
    val clock = system()
    val commands = mutableMapOf<String, () -> Boolean>(
        "declaration-review" to fun(): Boolean {
            val store = newMemoryDeclarationReviewStore()
            for (index in 0 until 9) {
                store.save(newDeclarationReview("declaration-review-$index", "Sample declaration review $index"))
            }
            val flow = newDeclarationReviewManager(store, clock)
            try {
                flow.open("declaration-review-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildDeclarationReviewReport("Declaration Review", items, clock)
            println(report.render())
            return flow.audit((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
        "broker-estimate" to fun(): Boolean {
            val store = newMemoryBrokerEstimateStore()
            for (index in 0 until 9) {
                store.save(newBrokerEstimate("broker-estimate-$index", "Sample broker estimate $index"))
            }
            val flow = newBrokerEstimateWorkflow(store, clock)
            try {
                flow.open("broker-estimate-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildBrokerEstimateReport("Broker Estimate", items, clock)
            println(report.render())
            return flow.reviewStale((System.currentTimeMillis() / 1000).toInt()) >= 0
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
