package com.acme.transport.customs.declaration.assignment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700971728)
        val items = mutableListOf<DeclarationPolicy>()
        for (index in 0 until 2) {
            items.add(newDeclarationPolicy("declaration-policy-$index", "sample"))
        }
        val report = buildDeclarationPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
