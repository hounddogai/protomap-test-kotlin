package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedBackhaulTemplateTest {
    @Test
    fun `starts in open`() {
        val item = newCappedBackhaulTemplate("code-651", "title-39")
        assertEquals(CappedBackhaulTemplateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newCappedBackhaulTemplate("code-651", "title-39")
        try {
            item.moveTo(CappedBackhaulTemplateStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedBackhaulTemplateStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedBackhaulTemplate("code-651", "title-39")
        item.status = CappedBackhaulTemplateStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedBackhaulTemplateStatus.OPEN) }
    }
}
