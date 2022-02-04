package br.com.dio.coinconverter.presentation.viewModel

import androidx.lifecycle.*
import br.com.dio.coinconverter.data.model.ExchangeResponseValue
import br.com.dio.coinconverter.domain.DeleteExchangeUseCase
import br.com.dio.coinconverter.domain.ListExchangeUseCase
import br.com.dio.coinconverter.domain.SaveExchangeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val listExchangeUseCase: ListExchangeUseCase,
    private val deleteExchangeUseCase: DeleteExchangeUseCase,
    private val saveExchangeUseCase: SaveExchangeUseCase
) : ViewModel(), LifecycleObserver {


    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state


    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()


    //@OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun getExchanges() {
        viewModelScope.launch {
            listExchangeUseCase()
                .flowOn(Dispatchers.Main)
                .onStart {
                    _state.value = State.Loading
                }
                .catch {
                    _state.value = State.Error(it)
                }
                .collect {
                    _state.value = State.Success(it)
                }
        }
    }


    fun deleteAllFromDb(exchange: ExchangeResponseValue) {
        viewModelScope.launch {
            deleteExchangeUseCase.execute(exchange)
                .flowOn(Dispatchers.Main)
                .onStart {
                    _state.value = State.Loading
                }
                .catch {
                    _state.value = State.Error(it)
                }
                .collect {
                    _state.value = State.Deleted

                }
        }
    }

    fun saveExchange(exchange: ExchangeResponseValue) {
        viewModelScope.launch {
            saveExchangeUseCase(exchange)
                .flowOn(Dispatchers.Main)
                .onStart {
                    //Show progress dialog
                    _state.value = State.Loading
                }
                .catch {
                    //Show an error
                    _state.value = State.Error(it)
                }
                .collect {
                    //Show the result
                    _state.value = State.Saved

                }
        }
    }

    fun onTaskSwiped(exchange: ExchangeResponseValue) = viewModelScope.launch {
        deleteAllFromDb(exchange)
        tasksEventChannel.send(TasksEvent.ShowUndoMessange(exchange))
    }

    fun onUndoDeleteClick(exchange: ExchangeResponseValue) = viewModelScope.launch {
        saveExchange(exchange)
    }

    sealed class TasksEvent {
        data class ShowUndoMessange(val exchange: ExchangeResponseValue) : TasksEvent()
    }


    sealed class State {
        object Loading : State()
        object Deleted : State()
        object Saved : State()

        data class Success(val list: List<ExchangeResponseValue>) : State()
        data class Error(val error: Throwable) : State()
    }
}