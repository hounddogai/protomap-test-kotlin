package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingQualificationNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingQualificationNotice("code-919", "name-875")
        val view = toRollingQualificationNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingQualificationNoticeViews(mutableListOf<RollingQualificationNotice>(item, item)).size, "views")
    }
}
