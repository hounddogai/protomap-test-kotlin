package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newAccessorialEstimate("code-961", "title-650")
        assertEquals(AccessorialEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAccessorialEstimate("code-961", "title-650")
        try {
            item.moveTo(AccessorialEstimateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialEstimateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAccessorialEstimate("code-961", "title-650")
        item.status = AccessorialEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(AccessorialEstimateStatus.CREATED) }
    }
}
