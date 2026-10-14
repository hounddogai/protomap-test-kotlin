package com.acme.transport.freightpay.audit.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryAuditOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryAuditOverrideStore()
        val item = newPrimaryAuditOverride("code-209", "label-170")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
