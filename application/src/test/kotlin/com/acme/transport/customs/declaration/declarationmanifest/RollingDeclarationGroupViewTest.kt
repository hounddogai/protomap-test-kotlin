package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingDeclarationGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingDeclarationGroup("code-13", "title-914")
        val view = toRollingDeclarationGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingDeclarationGroupViews(mutableListOf<RollingDeclarationGroup>(item, item)).size, "views")
    }
}
