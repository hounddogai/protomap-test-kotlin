package com.acme.transport.safety.coaching.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictCoachingExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictCoachingException("code-641", "label-105")
        val view = toStrictCoachingExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictCoachingExceptionViews(mutableListOf<StrictCoachingException>(item, item)).size, "views")
    }
}
