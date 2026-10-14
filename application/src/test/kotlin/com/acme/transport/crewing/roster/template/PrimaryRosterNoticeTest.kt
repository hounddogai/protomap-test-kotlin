package com.acme.transport.crewing.roster.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRosterNoticeTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryRosterNotice("code-362", "label-912")
        assertEquals(PrimaryRosterNoticeStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPrimaryRosterNotice("code-362", "label-912")
        try {
            item.moveTo(PrimaryRosterNoticeStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRosterNoticeStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryRosterNotice("code-362", "label-912")
        item.status = PrimaryRosterNoticeStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryRosterNoticeStatus.OPEN) }
    }
}
