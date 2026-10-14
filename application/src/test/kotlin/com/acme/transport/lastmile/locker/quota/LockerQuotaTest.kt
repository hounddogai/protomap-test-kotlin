package com.acme.transport.lastmile.locker.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerQuotaTest {
    @Test
    fun `starts in new`() {
        val item = newLockerQuota("code-975", "title-444")
        assertEquals(LockerQuotaStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLockerQuota("code-975", "title-444")
        try {
            item.moveTo(LockerQuotaStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerQuotaStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLockerQuota("code-975", "title-444")
        item.status = LockerQuotaStatus.FAILED
        assertFails("move") { item.moveTo(LockerQuotaStatus.NEW) }
    }
}
