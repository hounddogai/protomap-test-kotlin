package com.acme.transport.linehaul.backhaul.backhaultemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingBackhaulTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingBackhaulTemplateStore()
        val item = newRollingBackhaulTemplate("code-26", "name-803")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
