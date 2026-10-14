package com.acme.transport.customs.declaration.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDeclarationTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalDeclarationTemplate("code-491", "label-287")
        assertEquals(RegionalDeclarationTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRegionalDeclarationTemplate("code-491", "label-287")
        try {
            item.moveTo(RegionalDeclarationTemplateStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDeclarationTemplateStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalDeclarationTemplate("code-491", "label-287")
        item.status = RegionalDeclarationTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalDeclarationTemplateStatus.DRAFT) }
    }
}
