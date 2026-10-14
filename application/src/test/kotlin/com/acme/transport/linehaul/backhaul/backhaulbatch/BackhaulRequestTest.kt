package com.acme.transport.linehaul.backhaul.backhaulbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulRequestTest {
    @Test
    fun `starts in created`() {
        val item = newBackhaulRequest("code-251", "label-72")
        assertEquals(BackhaulRequestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBackhaulRequest("code-251", "label-72")
        try {
            item.moveTo(BackhaulRequestStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulRequestStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBackhaulRequest("code-251", "label-72")
        item.status = BackhaulRequestStatus.RETIRED
        assertFails("move") { item.moveTo(BackhaulRequestStatus.CREATED) }
    }
}
