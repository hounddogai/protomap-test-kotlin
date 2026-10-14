package com.acme.transport.customs.hold.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newHoldQuota("code-858", "name-787")
        assertEquals(HoldQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newHoldQuota("code-858", "name-787")
        try {
            item.moveTo(HoldQuotaStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldQuotaStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newHoldQuota("code-858", "name-787")
        item.status = HoldQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(HoldQuotaStatus.IDLE) }
    }
}
