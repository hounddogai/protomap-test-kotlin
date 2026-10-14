package com.acme.transport.freightpay.accessorial.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialTemplateTest {
    @Test
    fun `starts in open`() {
        val item = newAccessorialTemplate("code-618", "name-9")
        assertEquals(AccessorialTemplateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newAccessorialTemplate("code-618", "name-9")
        try {
            item.moveTo(AccessorialTemplateStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialTemplateStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAccessorialTemplate("code-618", "name-9")
        item.status = AccessorialTemplateStatus.ARCHIVED
        assertFails("move") { item.moveTo(AccessorialTemplateStatus.OPEN) }
    }
}
