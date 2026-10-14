package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newLeaseReturnPreference("code-937", "name-524")
        assertEquals(LeaseReturnPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLeaseReturnPreference("code-937", "name-524")
        try {
            item.moveTo(LeaseReturnPreferenceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnPreferenceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLeaseReturnPreference("code-937", "name-524")
        item.status = LeaseReturnPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(LeaseReturnPreferenceStatus.REQUESTED) }
    }
}
