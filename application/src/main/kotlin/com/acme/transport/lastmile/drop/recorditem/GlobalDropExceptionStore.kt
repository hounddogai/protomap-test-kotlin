package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global drop exceptions by code. */
interface GlobalDropExceptionStore {
    fun find(code: String): GlobalDropException?
    fun save(item: GlobalDropException)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDropException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDropExceptionStore(
    var items: MutableMap<String, GlobalDropException>,
    var order: MutableList<String>,
) : GlobalDropExceptionStore {
    override fun find(code: String): GlobalDropException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDropException) {
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

    override fun list(): MutableList<GlobalDropException> {
        val out = mutableListOf<GlobalDropException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDropExceptionStore(): GlobalDropExceptionStore {
    return MemoryGlobalDropExceptionStore(mutableMapOf<String, GlobalDropException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDropExceptionStore(
    var inner: GlobalDropExceptionStore,
    var cache: TtlCache<GlobalDropException>,
    var hits: Int,
    var misses: Int,
) : GlobalDropExceptionStore {
    override fun find(code: String): GlobalDropException? {
        val hit = (cache.get(code) as GlobalDropException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDropException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDropException> = inner.list()
}

fun newCachedGlobalDropExceptionStore(inner: GlobalDropExceptionStore, ttl: Int, clock: Clock): GlobalDropExceptionStore {
    return CachedGlobalDropExceptionStore(
        inner = inner,
        cache = newTtlCache<GlobalDropException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global drop exceptions. Empty fields match all. */
data class GlobalDropExceptionQuery(
    var lane: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDropException): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalDropException>): MutableList<GlobalDropException> {
        val matched = filter(items) { candidate: GlobalDropException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDropExceptions(store: GlobalDropExceptionStore, query: GlobalDropExceptionQuery): MutableList<GlobalDropException> {
    return query.apply(store.list())
}
