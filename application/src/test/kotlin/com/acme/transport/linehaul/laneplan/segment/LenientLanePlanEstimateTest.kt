package com.acme.transport.linehaul.laneplan.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientLanePlanEstimateTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientLanePlanEstimate("code-791", "title-945")
        assertEquals(LenientLanePlanEstimateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLenientLanePlanEstimate("code-791", "title-945")
        try {
            item.moveTo(LenientLanePlanEstimateStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientLanePlanEstimateStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientLanePlanEstimate("code-791", "title-945")
        item.status = LenientLanePlanEstimateStatus.CLOSED
        assertFails("move") { item.moveTo(LenientLanePlanEstimateStatus.DRAFT) }
    }
}
