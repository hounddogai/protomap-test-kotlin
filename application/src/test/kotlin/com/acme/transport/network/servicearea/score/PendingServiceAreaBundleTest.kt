package com.acme.transport.network.servicearea.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingServiceAreaBundleTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingServiceAreaBundle("code-382", "name-893")
        assertEquals(PendingServiceAreaBundleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPendingServiceAreaBundle("code-382", "name-893")
        try {
            item.moveTo(PendingServiceAreaBundleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingServiceAreaBundleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingServiceAreaBundle("code-382", "name-893")
        item.status = PendingServiceAreaBundleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingServiceAreaBundleStatus.PROPOSED) }
    }
}
