package com.acme.transport.lastmile.attempt.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default attempt notices by code. */
interface DefaultAttemptNoticeStore {
    fun find(code: String): DefaultAttemptNotice?
    fun save(item: DefaultAttemptNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultAttemptNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultAttemptNoticeStore(
    var items: MutableMap<String, DefaultAttemptNotice>,
    var order: MutableList<String>,
) : DefaultAttemptNoticeStore {
    override fun find(code: String): DefaultAttemptNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultAttemptNotice) {
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

    override fun list(): MutableList<DefaultAttemptNotice> {
        val out = mutableListOf<DefaultAttemptNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultAttemptNoticeStore(): DefaultAttemptNoticeStore {
    return MemoryDefaultAttemptNoticeStore(
        mutableMapOf<String, DefaultAttemptNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultAttemptNoticeStore(
    var inner: DefaultAttemptNoticeStore,
    var cache: TtlCache<DefaultAttemptNotice>,
    var hits: Int,
    var misses: Int,
) : DefaultAttemptNoticeStore {
    override fun find(code: String): DefaultAttemptNotice? {
        val hit = (cache.get(code) as DefaultAttemptNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultAttemptNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultAttemptNotice> = inner.list()
}

fun newCachedDefaultAttemptNoticeStore(inner: DefaultAttemptNoticeStore, ttl: Int, clock: Clock): DefaultAttemptNoticeStore {
    return CachedDefaultAttemptNoticeStore(
        inner = inner,
        cache = newTtlCache<DefaultAttemptNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default attempt notices. Empty fields match all. */
data class DefaultAttemptNoticeQuery(
    var unit: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultAttemptNotice): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultAttemptNotice>): MutableList<DefaultAttemptNotice> {
        val matched = filter(items) { candidate: DefaultAttemptNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultAttemptNotices(store: DefaultAttemptNoticeStore, query: DefaultAttemptNoticeQuery): MutableList<DefaultAttemptNotice> {
    return query.apply(store.list())
}
