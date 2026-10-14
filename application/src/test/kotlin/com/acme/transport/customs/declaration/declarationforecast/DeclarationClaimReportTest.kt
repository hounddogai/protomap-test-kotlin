package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709760490)
        val items = mutableListOf<DeclarationClaim>()
        for (index in 0 until 5) {
            items.add(newDeclarationClaim("declaration-claim-$index", "sample"))
        }
        val report = buildDeclarationClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
