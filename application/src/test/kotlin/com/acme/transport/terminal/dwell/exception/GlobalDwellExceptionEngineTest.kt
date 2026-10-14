package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDwellExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDwellExceptionEngine(newRegistry())
        val item = newGlobalDwellException("code-576", "title-456")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
