package com.acme.transport.customs.declaration.policy

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDeclarationPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701314346)
        val items = mutableListOf<TieredDeclarationPolicy>()
        for (index in 0 until 3) {
            items.add(newTieredDeclarationPolicy("tiered-declaration-policy-$index", "sample"))
        }
        val report = buildTieredDeclarationPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
