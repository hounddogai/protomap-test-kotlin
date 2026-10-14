package com.acme.transport.customs.clearance.slot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default clearance targets by code. */
interface DefaultClearanceTargetStore {
    fun find(code: String): DefaultClearanceTarget?
    fun save(item: DefaultClearanceTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultClearanceTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultClearanceTargetStore(
    var items: MutableMap<String, DefaultClearanceTarget>,
    var order: MutableList<String>,
) : DefaultClearanceTargetStore {
    override fun find(code: String): DefaultClearanceTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultClearanceTarget) {
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

    override fun list(): MutableList<DefaultClearanceTarget> {
        val out = mutableListOf<DefaultClearanceTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultClearanceTargetStore(): DefaultClearanceTargetStore {
    return MemoryDefaultClearanceTargetStore(
        mutableMapOf<String, DefaultClearanceTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultClearanceTargetStore(
    var inner: DefaultClearanceTargetStore,
    var cache: TtlCache<DefaultClearanceTarget>,
    var hits: Int,
    var misses: Int,
) : DefaultClearanceTargetStore {
    override fun find(code: String): DefaultClearanceTarget? {
        val hit = (cache.get(code) as DefaultClearanceTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultClearanceTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultClearanceTarget> = inner.list()
}

fun newCachedDefaultClearanceTargetStore(inner: DefaultClearanceTargetStore, ttl: Int, clock: Clock): DefaultClearanceTargetStore {
    return CachedDefaultClearanceTargetStore(
        inner = inner,
        cache = newTtlCache<DefaultClearanceTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default clearance targets. Empty fields match all. */
data class DefaultClearanceTargetQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultClearanceTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultClearanceTarget>): MutableList<DefaultClearanceTarget> {
        val matched = filter(items) { candidate: DefaultClearanceTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultClearanceTargets(store: DefaultClearanceTargetStore, query: DefaultClearanceTargetQuery): MutableList<DefaultClearanceTarget> {
    return query.apply(store.list())
}
