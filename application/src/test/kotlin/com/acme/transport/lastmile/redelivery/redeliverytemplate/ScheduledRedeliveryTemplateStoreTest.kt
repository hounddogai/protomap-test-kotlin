package com.acme.transport.lastmile.redelivery.redeliverytemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledRedeliveryTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledRedeliveryTemplateStore()
        val item = newScheduledRedeliveryTemplate("code-812", "name-820")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
