package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryFuelSurchargeDraftTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryFuelSurchargeDraft("code-636", "name-786")
        assertEquals(PrimaryFuelSurchargeDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newPrimaryFuelSurchargeDraft("code-636", "name-786")
        try {
            item.moveTo(PrimaryFuelSurchargeDraftStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryFuelSurchargeDraftStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryFuelSurchargeDraft("code-636", "name-786")
        item.status = PrimaryFuelSurchargeDraftStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryFuelSurchargeDraftStatus.NEW) }
    }
}
