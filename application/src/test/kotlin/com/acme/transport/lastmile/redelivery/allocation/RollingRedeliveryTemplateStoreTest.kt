package com.acme.transport.lastmile.redelivery.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingRedeliveryTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingRedeliveryTemplateStore()
        val item = newRollingRedeliveryTemplate("code-505", "label-743")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
