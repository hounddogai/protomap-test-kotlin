package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialAllocationTest {
    @Test
    fun `starts in created`() {
        val item = newAccessorialAllocation("code-339", "title-735")
        assertEquals(AccessorialAllocationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAccessorialAllocation("code-339", "title-735")
        try {
            item.moveTo(AccessorialAllocationStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialAllocationStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAccessorialAllocation("code-339", "title-735")
        item.status = AccessorialAllocationStatus.RETIRED
        assertFails("move") { item.moveTo(AccessorialAllocationStatus.CREATED) }
    }
}
