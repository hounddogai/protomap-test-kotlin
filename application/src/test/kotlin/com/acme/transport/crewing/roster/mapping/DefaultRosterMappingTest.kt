package com.acme.transport.crewing.roster.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRosterMappingTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultRosterMapping("code-86", "label-247")
        assertEquals(DefaultRosterMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDefaultRosterMapping("code-86", "label-247")
        try {
            item.moveTo(DefaultRosterMappingStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRosterMappingStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultRosterMapping("code-86", "label-247")
        item.status = DefaultRosterMappingStatus.FAILED
        assertFails("move") { item.moveTo(DefaultRosterMappingStatus.NEW) }
    }
}
