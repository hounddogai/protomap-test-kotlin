package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaScoreTest {
    @Test
    fun `starts in requested`() {
        val item = newServiceAreaScore("code-665", "label-884")
        assertEquals(ServiceAreaScoreStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newServiceAreaScore("code-665", "label-884")
        try {
            item.moveTo(ServiceAreaScoreStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaScoreStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newServiceAreaScore("code-665", "label-884")
        item.status = ServiceAreaScoreStatus.EXPIRED
        assertFails("move") { item.moveTo(ServiceAreaScoreStatus.REQUESTED) }
    }
}
