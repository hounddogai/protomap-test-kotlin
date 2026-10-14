package com.acme.transport.safety.nearmiss.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss exceptions by code. */
interface NearMissExceptionStore {
    fun find(code: String): NearMissException?
    fun save(item: NearMissException)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissExceptionStore(
    var items: MutableMap<String, NearMissException>,
    var order: MutableList<String>,
) : NearMissExceptionStore {
    override fun find(code: String): NearMissException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissException) {
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

    override fun list(): MutableList<NearMissException> {
        val out = mutableListOf<NearMissException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissExceptionStore(): NearMissExceptionStore {
    return MemoryNearMissExceptionStore(mutableMapOf<String, NearMissException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissExceptionStore(
    var inner: NearMissExceptionStore,
    var cache: TtlCache<NearMissException>,
    var hits: Int,
    var misses: Int,
) : NearMissExceptionStore {
    override fun find(code: String): NearMissException? {
        val hit = (cache.get(code) as NearMissException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissException> = inner.list()
}

fun newCachedNearMissExceptionStore(inner: NearMissExceptionStore, ttl: Int, clock: Clock): NearMissExceptionStore {
    return CachedNearMissExceptionStore(
        inner = inner,
        cache = newTtlCache<NearMissException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss exceptions. Empty fields match all. */
data class NearMissExceptionQuery(
    var segment: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissException): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<NearMissException>): MutableList<NearMissException> {
        val matched = filter(items) { candidate: NearMissException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissExceptions(store: NearMissExceptionStore, query: NearMissExceptionQuery): MutableList<NearMissException> {
    return query.apply(store.list())
}
