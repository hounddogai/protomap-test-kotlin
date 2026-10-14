package com.acme.transport.lastmile.locker.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerAuditTest {
    @Test
    fun `starts in created`() {
        val item = newLockerAudit("code-663", "name-115")
        assertEquals(LockerAuditStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLockerAudit("code-663", "name-115")
        try {
            item.moveTo(LockerAuditStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerAuditStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLockerAudit("code-663", "name-115")
        item.status = LockerAuditStatus.RETIRED
        assertFails("move") { item.moveTo(LockerAuditStatus.CREATED) }
    }
}
