package com.acme.transport.emissions.idling.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingTemplateTest {
    @Test
    fun `starts in proposed`() {
        val item = newIdlingTemplate("code-261", "label-841")
        assertEquals(IdlingTemplateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newIdlingTemplate("code-261", "label-841")
        try {
            item.moveTo(IdlingTemplateStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingTemplateStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newIdlingTemplate("code-261", "label-841")
        item.status = IdlingTemplateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(IdlingTemplateStatus.PROPOSED) }
    }
}
