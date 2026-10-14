package com.acme.transport.emissions.idling.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingAuditTest {
    @Test
    fun `starts in created`() {
        val item = newIdlingAudit("code-505", "label-837")
        assertEquals(IdlingAuditStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newIdlingAudit("code-505", "label-837")
        try {
            item.moveTo(IdlingAuditStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingAuditStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newIdlingAudit("code-505", "label-837")
        item.status = IdlingAuditStatus.RETIRED
        assertFails("move") { item.moveTo(IdlingAuditStatus.CREATED) }
    }
}
