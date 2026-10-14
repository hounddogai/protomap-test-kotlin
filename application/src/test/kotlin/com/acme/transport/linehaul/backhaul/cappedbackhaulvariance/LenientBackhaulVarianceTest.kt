package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientBackhaulVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newLenientBackhaulVariance("code-357", "label-413")
        assertEquals(LenientBackhaulVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientBackhaulVariance("code-357", "label-413")
        try {
            item.moveTo(LenientBackhaulVarianceStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientBackhaulVarianceStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientBackhaulVariance("code-357", "label-413")
        item.status = LenientBackhaulVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(LenientBackhaulVarianceStatus.CREATED) }
    }
}
