package com.acme.transport.fleetops.leasereturn.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingLeaseReturnGroupTest {
    @Test
    fun `starts in created`() {
        val item = newRollingLeaseReturnGroup("code-937", "name-768")
        assertEquals(RollingLeaseReturnGroupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRollingLeaseReturnGroup("code-937", "name-768")
        try {
            item.moveTo(RollingLeaseReturnGroupStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingLeaseReturnGroupStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingLeaseReturnGroup("code-937", "name-768")
        item.status = RollingLeaseReturnGroupStatus.RETIRED
        assertFails("move") { item.moveTo(RollingLeaseReturnGroupStatus.CREATED) }
    }
}
