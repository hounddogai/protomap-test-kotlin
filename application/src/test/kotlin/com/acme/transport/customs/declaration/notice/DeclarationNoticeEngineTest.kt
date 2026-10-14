package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationNoticeEngine(newRegistry())
        val item = newDeclarationNotice("code-895", "title-756")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
