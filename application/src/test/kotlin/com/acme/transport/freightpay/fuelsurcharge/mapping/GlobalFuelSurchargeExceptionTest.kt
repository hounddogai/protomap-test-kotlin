package com.acme.transport.freightpay.fuelsurcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalFuelSurchargeExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalFuelSurchargeException("code-252", "name-663")
        assertEquals(GlobalFuelSurchargeExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newGlobalFuelSurchargeException("code-252", "name-663")
        try {
            item.moveTo(GlobalFuelSurchargeExceptionStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalFuelSurchargeExceptionStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalFuelSurchargeException("code-252", "name-663")
        item.status = GlobalFuelSurchargeExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalFuelSurchargeExceptionStatus.PLANNED) }
    }
}
