package com.acme.transport.lastmile.drop.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDropDraftTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultDropDraft("code-660", "label-737")
        assertEquals(DefaultDropDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newDefaultDropDraft("code-660", "label-737")
        try {
            item.moveTo(DefaultDropDraftStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDropDraftStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultDropDraft("code-660", "label-737")
        item.status = DefaultDropDraftStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultDropDraftStatus.CREATED) }
    }
}
