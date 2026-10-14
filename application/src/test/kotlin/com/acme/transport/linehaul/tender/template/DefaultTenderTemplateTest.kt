package com.acme.transport.linehaul.tender.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTenderTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultTenderTemplate("code-784", "name-683")
        assertEquals(DefaultTenderTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultTenderTemplate("code-784", "name-683")
        try {
            item.moveTo(DefaultTenderTemplateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTenderTemplateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultTenderTemplate("code-784", "name-683")
        item.status = DefaultTenderTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultTenderTemplateStatus.CREATED) }
    }
}
