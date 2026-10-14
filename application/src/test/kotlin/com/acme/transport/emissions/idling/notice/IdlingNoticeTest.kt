package com.acme.transport.emissions.idling.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingNoticeTest {
    @Test
    fun `starts in created`() {
        val item = newIdlingNotice("code-715", "title-194")
        assertEquals(IdlingNoticeStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newIdlingNotice("code-715", "title-194")
        try {
            item.moveTo(IdlingNoticeStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingNoticeStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newIdlingNotice("code-715", "title-194")
        item.status = IdlingNoticeStatus.RETIRED
        assertFails("move") { item.moveTo(IdlingNoticeStatus.CREATED) }
    }
}
