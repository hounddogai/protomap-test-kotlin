package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global dwell exceptions by code. */
interface GlobalDwellExceptionStore {
    fun find(code: String): GlobalDwellException?
    fun save(item: GlobalDwellException)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDwellException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDwellExceptionStore(
    var items: MutableMap<String, GlobalDwellException>,
    var order: MutableList<String>,
) : GlobalDwellExceptionStore {
    override fun find(code: String): GlobalDwellException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDwellException) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<GlobalDwellException> {
        val out = mutableListOf<GlobalDwellException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDwellExceptionStore(): GlobalDwellExceptionStore {
    return MemoryGlobalDwellExceptionStore(
        mutableMapOf<String, GlobalDwellException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDwellExceptionStore(
    var inner: GlobalDwellExceptionStore,
    var cache: TtlCache<GlobalDwellException>,
    var hits: Int,
    var misses: Int,
) : GlobalDwellExceptionStore {
    override fun find(code: String): GlobalDwellException? {
        val hit = (cache.get(code) as GlobalDwellException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDwellException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDwellException> = inner.list()
}

fun newCachedGlobalDwellExceptionStore(inner: GlobalDwellExceptionStore, ttl: Int, clock: Clock): GlobalDwellExceptionStore {
    return CachedGlobalDwellExceptionStore(
        inner = inner,
        cache = newTtlCache<GlobalDwellException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global dwell exceptions. Empty fields match all. */
data class GlobalDwellExceptionQuery(
    var channel: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDwellException): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalDwellException>): MutableList<GlobalDwellException> {
        val matched = filter(items) { candidate: GlobalDwellException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDwellExceptions(store: GlobalDwellExceptionStore, query: GlobalDwellExceptionQuery): MutableList<GlobalDwellException> {
    return query.apply(store.list())
}
