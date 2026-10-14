package com.acme.transport.terminal.dwell.dwelltemplate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DwellExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellExceptionEngine(newRegistry())
        val item = newDwellException("code-571", "name-25")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
