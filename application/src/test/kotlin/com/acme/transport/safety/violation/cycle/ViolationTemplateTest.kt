package com.acme.transport.safety.violation.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationTemplateTest {
    @Test
    fun `starts in proposed`() {
        val item = newViolationTemplate("code-896", "label-353")
        assertEquals(ViolationTemplateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newViolationTemplate("code-896", "label-353")
        try {
            item.moveTo(ViolationTemplateStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationTemplateStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newViolationTemplate("code-896", "label-353")
        item.status = ViolationTemplateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ViolationTemplateStatus.PROPOSED) }
    }
}
