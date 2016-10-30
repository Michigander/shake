/* 

Sensor - run on watch, listen for button press, relay message to js script.

*/
 
/* sensor.c : TO BE USED WITH relay.js */
#include <pebble.h>
#include <time.h>

// Definitions 
#define PAIRING 0
#define JSREADY 1

static const int32_t ONPRESS = 2; 
static const int NUM_SAMPLES = 3;  

// Display 
static Window *s_main_window;
static TextLayer *s_output_layer;
static int session_window = 75;

// Status 
int button_alive = 0; 

static bool js_state;
bool js_is_ready() {
  
  return js_state;   
  
}

/* -- MESSAGE HANDLERS -- */

/* Handle a message in the inbox */
static void inbox_received_callback(DictionaryIterator *iterator, void *context) {

  // handle initial launch of js
  Tuple *ready_tuple = dict_find(iterator, JSREADY);
  if( ready_tuple ){
    text_layer_set_text(s_output_layer, "Sensing for contacts");
    js_state = true;
  }

  // message from .js 
  static char pairing_buffer[32];

  // get tuple
  Tuple *pairing_tuple = dict_find(iterator, PAIRING);
  if( pairing_tuple ){
  
    // store in buffer
    snprintf(pairing_buffer, sizeof( pairing_buffer ), "%s", pairing_tuple->value->cstring);
  
    // display result 
    text_layer_set_text(s_output_layer, pairing_buffer);
    
  }
  
  APP_LOG(APP_LOG_LEVEL_INFO, "Inbox Received");
  
}

/* Handle lost inbox message */
static void inbox_dropped_callback(AppMessageResult reason, void *context) {

  APP_LOG(APP_LOG_LEVEL_ERROR, "Message dropped!");

}

/* Handle lost outbox message */
static void outbox_failed_callback(DictionaryIterator *iterator, AppMessageResult reason, void *context) {

  APP_LOG(APP_LOG_LEVEL_ERROR, "Outbox send failed!");

}

/* Handle successful outbox message */
static void outbox_sent_callback(DictionaryIterator *iterator, void *context) {

  APP_LOG(APP_LOG_LEVEL_INFO, "Outbox send success!");
 
}

/* -- BUTTON CLICKS -- */

/* 

  Handle watch button clicks. 
  Request a pair upon "select" button click.
  
*/
static void select_click_handler(ClickRecognizerRef recognizer, void *context) {

  // ensure javascript is ready
  if( !js_is_ready() ){
    vibes_short_pulse();
    text_layer_set_text(s_output_layer, "JS Not Ready");
    return;  
  }
  
  // notify 
  vibes_short_pulse();
  text_layer_set_text(s_output_layer, "Pairing");
  
  /* Request to pair */

  // message dictionary
  DictionaryIterator *iter;
  app_message_outbox_begin(&iter);
  
  if(!iter){

    return; // error 

  }

  // fill
  int USER = 0;
  dict_write_int(iter,1,&USER,sizeof(int),true);
  dict_write_end(iter);
  
  // send
  app_message_outbox_send();
   
}

/* Configure button click handling */
static void click_config_provider(void *context) {
  
  // register click handler for "select"
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
 
}

/* -- WINDOW -- */ 

static void window_load(Window *window) {

  Layer *window_layer = window_get_root_layer(window);
  GRect window_bounds = layer_get_bounds(window_layer);

  // Create output TextLayer
  s_output_layer = text_layer_create(GRect(5, 0, window_bounds.size.w - 10, window_bounds.size.h));
  text_layer_set_font(s_output_layer, fonts_get_system_font(FONT_KEY_GOTHIC_24));
  text_layer_set_text(s_output_layer, "Welcome - waiting for js to load ... ");
  text_layer_set_overflow_mode(s_output_layer, GTextOverflowModeWordWrap);
  layer_add_child(window_layer, text_layer_get_layer(s_output_layer));

}

static void window_unload(Window *window) {

  // Destroy output TextLayer
  text_layer_destroy(s_output_layer);
  
}

/* -- MAIN COMPONENTS -- */

static void init() {
  
  // Create main Window
  s_main_window = window_create();
  window_set_window_handlers(s_main_window, (WindowHandlers) {
      .load = window_load,
    .unload = window_unload
	});
  window_set_click_config_provider(s_main_window, click_config_provider);
  window_stack_push(s_main_window, true);
   
  // Register callbacks 
  app_message_register_inbox_received(inbox_received_callback);
  app_message_register_inbox_dropped(inbox_dropped_callback);
  app_message_register_outbox_failed(outbox_failed_callback);
  app_message_register_outbox_sent(outbox_sent_callback);
  
  // Open app message with buffer size 
  app_message_open(62,62);
  
}

static void deinit() {
  
  // Destroy main Window
  window_destroy(s_main_window);  
  
}

int main(void) {
  
  init();
  app_event_loop();
  deinit();
  
}
