package com.acme.transport.customs.declaration.declarationsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDeclarationAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDeclarationAssignment("code-361", "name-416")
        val view = toSeasonalDeclarationAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDeclarationAssignmentViews(mutableListOf<SeasonalDeclarationAssignment>(item, item)).size, "views")
    }
}
