package com.acme.transport.network.zone.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneBatchTest {
    @Test
    fun `starts in proposed`() {
        val item = newZoneBatch("code-106", "title-692")
        assertEquals(ZoneBatchStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newZoneBatch("code-106", "title-692")
        try {
            item.moveTo(ZoneBatchStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneBatchStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newZoneBatch("code-106", "title-692")
        item.status = ZoneBatchStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ZoneBatchStatus.PROPOSED) }
    }
}
