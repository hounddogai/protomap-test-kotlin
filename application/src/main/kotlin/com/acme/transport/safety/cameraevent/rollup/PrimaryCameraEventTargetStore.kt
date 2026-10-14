package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary camera event targets by code. */
interface PrimaryCameraEventTargetStore {
    fun find(code: String): PrimaryCameraEventTarget?
    fun save(item: PrimaryCameraEventTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCameraEventTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCameraEventTargetStore(
    var items: MutableMap<String, PrimaryCameraEventTarget>,
    var order: MutableList<String>,
) : PrimaryCameraEventTargetStore {
    override fun find(code: String): PrimaryCameraEventTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCameraEventTarget) {
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

    override fun list(): MutableList<PrimaryCameraEventTarget> {
        val out = mutableListOf<PrimaryCameraEventTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCameraEventTargetStore(): PrimaryCameraEventTargetStore {
    return MemoryPrimaryCameraEventTargetStore(
        mutableMapOf<String, PrimaryCameraEventTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCameraEventTargetStore(
    var inner: PrimaryCameraEventTargetStore,
    var cache: TtlCache<PrimaryCameraEventTarget>,
    var hits: Int,
    var misses: Int,
) : PrimaryCameraEventTargetStore {
    override fun find(code: String): PrimaryCameraEventTarget? {
        val hit = (cache.get(code) as PrimaryCameraEventTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCameraEventTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCameraEventTarget> = inner.list()
}

fun newCachedPrimaryCameraEventTargetStore(inner: PrimaryCameraEventTargetStore, ttl: Int, clock: Clock): PrimaryCameraEventTargetStore {
    return CachedPrimaryCameraEventTargetStore(
        inner = inner,
        cache = newTtlCache<PrimaryCameraEventTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary camera event targets. Empty fields match all. */
data class PrimaryCameraEventTargetQuery(
    var category: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCameraEventTarget): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCameraEventTarget>): MutableList<PrimaryCameraEventTarget> {
        val matched = filter(items) { candidate: PrimaryCameraEventTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCameraEventTargets(store: PrimaryCameraEventTargetStore, query: PrimaryCameraEventTargetQuery): MutableList<PrimaryCameraEventTarget> {
    return query.apply(store.list())
}
